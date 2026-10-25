import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import SizeTips from '.';
import { IntlWrapper } from 'components/utils/wrapper';

it('SizeTips snapshot', async () => {
  await (async () => {
    const { asFragment, rerender } = render(
      <SizeTips
        hide={false}
        onConfirm={function (): void {
          throw new Error('Function not implemented.');
        }}
        size={0}
      />,
      {
        wrapper: IntlWrapper,
      }
    );
    expect(asFragment()).toMatchSnapshot();

    rerender(
      <SizeTips
        hide={true}
        onConfirm={function (): void {
          throw new Error('Function not implemented.');
        }}
        size={0}
      />
    );
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('click SizeTips', async () => {
  await (async () => {
    const handleConfirm = vi.fn();
    render(<SizeTips onConfirm={handleConfirm} size={0} hide={false}></SizeTips>, {
      wrapper: IntlWrapper,
    });

    await userEvent.click(screen.getByText('I see'));
    expect(handleConfirm).toBeCalled();
  })();
});
